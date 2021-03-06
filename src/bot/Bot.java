package bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.vdurmont.emoji.EmojiParser;

public class Bot extends TelegramLongPollingBot {

	int numberOfDice = 0, numberOfSides = 0, rollModifier = 0, diceToKeep = 0, rollResult = 0;
	SendMessage sendMessage;
	SendVideo sendVideo;
	SendPhoto sendPhoto;
	SendDocument sendDocument;

	@Override
	public String getBotUsername() {
		// SUSTITUIR POR EL ALIAS DE TU BOT
		return "";
	}

	@Override
	public void onUpdateReceived(Update arg0) {

		// Almacenar la ID del chat para poder contestar
		// sendMessage = new SendMessage().setChatId(arg0.getMessage().getChatId());
		sendMessage = new SendMessage().setChatId(arg0.getMessage().getChatId());
		sendMessage.setReplyToMessageId(arg0.getMessage().getMessageId());
		sendVideo = new SendVideo().setChatId(arg0.getMessage().getChatId());
		sendPhoto = new SendPhoto().setChatId(arg0.getMessage().getChatId());
		// sendVideo.setReplyToMessageId(arg0.getMessage().getMessageId());

		// Imprimir por consola el mensaje recibido
		if (arg0.getMessage().hasText()) {
			System.out.println("\nMENSAJE RECIBIDO DE @" + arg0.getMessage().getFrom().getUserName() + ": "
					+ arg0.getMessage().getText().toLowerCase());

			// Si el mensaje es un comando, se intenta averiguar cual
			if (arg0.getMessage().getText().startsWith("/")) {
				findCommand(arg0.getMessage().getText().toLowerCase().substring(1));
			} else {
				// En caso contrario responde con un mensaje, ayudando a usar el bot
				sendMessage.setText("Hola " + arg0.getMessage().getFrom().getFirstName()
						+ " , Por ahora solo respondo a la tirada de dados, escribe /ayuda para más información");
			}
		}
		if (arg0.getMessage().hasDocument()) {
			System.out.println("Document: " + arg0.getMessage().getDocument());
		}
		if (arg0.getMessage().hasPhoto()) {
			List<PhotoSize> fotos = arg0.getMessage().getPhoto();
			for (PhotoSize foto : fotos) {
				System.out.println("Datos foto: " + foto);
			}
		}

	}

	public void answerUser() {
		// Respuesta del bot centralizada, todas las respuestas se mandan desde aquí
		// también se escribe un mensaje por consola
		try {
			System.out.println("RESPUESTA: 🎲" + sendMessage.getText());
			sendMessage.setText(EmojiParser.parseToUnicode("🎲" + sendMessage.getText()));
			sendMessage.setParseMode("Markdown");
			execute(sendMessage);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getBotToken() {
		// SUSTITUIR POR EL TOKEN DE TU BOT
		return "";
	}

	private void findCommand(String command) {
		// Comandos admitidos
		if (command.matches("(ayuda|help|start|test)")) {
			sendMessage.setText("Con este bot puedes lanzar una serie de dados ¡Y cada vez más!\n"
					+ "Simples [/d6]\nVarios dados [/3d6]\nCon modificador [/3d10+5 /2d12-7]\nDados explosivos [/1d20!+3]\nManteniendo dados [/3d20!h1 /6d10kh2 /4d8l1]\nSavage Worlds [/s-2 /s4 /s6 /s8 /s10 /s12]\nIn Nomine Satanis [/ins]\nVampiro [/vampiro6 /vamp7d8 /v10d4]\nFate/Fudge [/f /fate+5 /fudge-7]\nHitos [/h /h+7 /hitos /hitos+5]\nPbtA [/pbta /pbta+1 /p-2 /p+1]\nEste bot es de código abierto -> https://github.com/HonzoNebro/MiReinoBot");
			sendMessage.setParseMode("Markdown");
			answerUser();
		}
		// Dado único
		else if (command.matches("[d]\\d+")) {
			// d6
			// es una tirada de un solo dado
			simpleRoll(command);
		} else if (command.matches("[d]\\d+[!]")) {
			// d6!
			// es una tirada de un solo dado explosivo
			explosiveRoll(command);
		} else if (command.matches("[d]\\d+[+-]\\d+")) {
			// d5+6 d3-21
			// es una tirada de un dado con modificador
			simpleRollWithModifier(command);
		} else if (command.matches("[d]\\d+[!][+-]\\d+")) {
			// d5!+6 d3!+21
			// es una tirada de un dado explosivo con modificador
			simpleExplosiveRollWithModifier(command);
		}

		// Varios dados
		else if (command.matches("\\d+[d]\\d+")) {
			// 2d6
			// es una tirada de varios dados
			severalDice(command);
		} else if (command.matches("\\d+[d]\\d+[!]")) {
			// 2d6!
			// es una tirada de varios dados explosivos
			severalExplosiveDice(command);
		} else if (command.matches("\\d+[d]\\d+[+-]\\d+")) {
			// 3d5+6 10d3-21
			// es una tirada de varios dados con modificador
			severalDiceWithModifier(command);
		} else if (command.matches("\\d+[d]\\d+[!][+-]\\d+")) {
			// 3d5!+6 10d3!-21
			// es una tirada de varios dados explosivos con modificador
			severalExplosiveDiceWithModifier(command);
		}

		// Tirada de varios dados, donde guardamos los dados más altos
		else if (command.matches("\\d+[d]\\d+(h|kh|l|kl)\\d+")) {
			// 3d5h1 3d5kh1 3d5Kh1 3d5KH1 3d5kH1 3d5kH1
			// es una tirada, de 3d5 donde guardamos dados
			severalKeepRoll(command);
		} else if (command.matches("\\d+[d]\\d+[!](h|kh|l|kl)\\d+")) {
			// 3d5!h1
			// es una tirada, de 3d5 explosivos donde guardamos dados
			severalExplosiveKeepRoll(command);
		} else if (command.matches("\\d+[d]\\d+(h|kh|l|kl)\\d+[+-]\\d+")) {
			// 3d5l1+5
			// es una tirada, de 3d5 donde guardamos dados y
			// aplicamos el modificador
			severalKeepRollWithModifier(command);
		} else if (command.matches("\\d+[d]\\d+[!](h|kh|l|kl)\\d+[+-]\\d+")) {
			// 3d5!l1+5
			// es una tirada, de 3d5 explosivos donde guardamos dados y
			// aplicamos el modificador
			severalExplosiveKeepRollWithModifier(command);
		}

		// Tirada múltiple de un solo dado
		else if (command.matches("\\d+[x][d]\\d+")) {
			// 3xd5
			// es una tirada multiple, de 1d5 cada una
		} else if (command.matches("\\d+[x][d]\\d+[!]")) {
			// 3xd5!
			// es una tirada multiple, de 1d5 explosivo cada una
		} else if (command.matches("\\d+[x][d]\\d+[+-]\\d+")) {
			// 3xd5+10
			// es una tirada multiple, de 1d5 cada una
			// donde aplicamos modificador
		} else if (command.matches("\\d+[x][d]\\d+[!][+-]\\d+")) {
			// 3xd5!+10
			// es una tirada multiple, de 1d5 explosivo cada una
			// donde aplicamos modificador
		}

		// Tirada múltiple de varios dados
		else if (command.matches("\\d+[x]\\d+[d]\\d+")) {
			// 6x4d6
			// es una tirada multiple, de 4d6 cada una
		} else if (command.matches("\\d+[x]\\d+[d]\\d+[!]")) {
			// 6x4d6!
			// es una tirada multiple, de 4d6 explosivos cada una
		} else if (command.matches("\\d+[x]\\d+[d]\\d+[+-]\\d+")) {
			// 3x2d5+10
			// es una tirada multiple, de 2d5 cada una
			// donde aplicamos modificador
		} else if (command.matches("\\d+[x]\\d+[d]\\d+[!][+-]\\d+")) {
			// 3x2d5!+10
			// es una tirada multiple, de 2d5 explosivos cada una
			// donde aplicamos modificador
		}

		// Tirada multiple de varios dados donde guardamos dados **NECESITA
		// REPLANTEARSE**
		else if (command.matches("\\d+[x]\\d+[d]\\d+(h|kh|l|kl)\\d+")) {
			// 6x4d6h3
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
		} else if (command.matches("\\d+[x]\\d+[d]\\d+[!](h|kh|l|kl)\\d+")) {
			// 6x4d6!h3
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
		} else if (command.matches("\\d+[x]\\d+[d]\\d+(h|kh|l|kl)\\d+[+-]\\d+")) {
			// 6x4d6l3+5
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
			// y aplicamos el modificador
		} else if (command.matches("\\d+[x]\\d+[d]\\d+[!](h|kh|l|kl)\\d+[+-]\\d+")) {
			// 6x4d6!l3+5
			// es una tirada multiple, de 4d6 donde nos quedamos con el más alto
			// y aplicamos el modificador
		}

		// Tirada de Fate
		else if (command.matches("(f|fate|fudge)")) {
			// es una tirada de Fate
			fateRoll(command);
		} else if (command.matches("(f|fate|fudge)[+-]\\d+")) {
			// esta es una tirada fate con modificador
			fateRollWithModifier(command);
		}

		// Tirada de Hitos
		else if (command.matches("(h|hitos)")) {
			hitosRoll(command);
			// esta es una tirada Hitos con modificador
		} else if (command.matches("(h|hitos)[+]\\d+")) {
			// esta es una tirada Hitos con modificador
			hitosRollWithModifier(command);
		}

		// Tirada de Savage Worlds
		else if (command.matches("(s|savage)(-2|4|6|8|10|12)")) {
			// roll {1d6+1d8}kh1
			savageWorldRoll(command);
		}

		// Tirada de In Nomine Santis
		else if (command.matches("(ins)")) {
			// roll 3d6 sin ordenar
			inNomineSatanisRoll(command);
		}

		// Tirada de Vampiro
		else if (command.matches("(v|vampiro|vamp)\\d+")) {
			vampireRoll(command);
		}
		// Tirada de Vampiro con dificultad personalizada
		else if (command.matches("(v|vampiro|vamp)\\d+([d]\\d+)?")) {
			vampireRollWithCustomDificulty(command);
		}

		// Tirada de Powered by the Apocalypse
		else if (command.matches("(pbta|p)")) {
			// pbta p
			PoweredByTheApocapilse(command);
		}
		// Tirada de Powered by the Apocalypse
		else if (command.matches("(pbta|p)[+-]\\d+")) {
			// pbta+1 p-3
			PoweredByTheApocapilseWithModifier(command);
		}

		// Otros comandos
		else {
			sendMessage.setText("Comando no reconocido. Escribe /ayuda para más información");
			answerUser();
		}
		/*
		 * TODO
		 * 
		 * higher y lower por encima de la cantidad de dados o 0 dados se cogen todos y
		 * ya 3d5l0 3d5h10 l y h pasan a DropLowest y KeepLowest (dl, kl) y DropHighest
		 * y Keephighest (dh, kh)
		 * 
		 * expresiones regulares por detectar
		 * 
		 * tiradas shadowrun
		 * 
		 * tiradas hackmaster/dados penetrantes (5d6!p) cualquier tirada que hagas, si
		 * sacas el máximo en el dado vuelves a tirar y sumas (con un -1 al nuevo dado)
		 * y si vuelves a sacar otra vez el máximo vuelves a tirar...
		 * 
		 * 
		 * reroll (relanzar) 3d6r<3
		 * 
		 * agrupar
		 * 
		 * texto (3d10 Tirada hitos) sladría algo tipo 4,4,6 Tirada hitos
		 * 
		 * https://wiki.roll20.net/Dice_Reference
		 * 
		 */
	}

	private void simpleRoll(String command) {
		// d5
		numberOfSides = Integer.parseInt(command.substring(1));
		rollResult = 0;
		if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = (int) (Math.random() * numberOfSides + 1);
			dice.add(valor);
			rollResult += valor;
			Collections.sort(dice);
			sendMessage.setText("*[d" + numberOfSides + "]*-> [" + dice + "] = *" + rollResult + "*");
			sendMessage.setParseMode("Markdown");
		}
		answerUser();
	}

	private void explosiveRoll(String command) {
		// d5!
		String[] numericParts = command.split("\\D+");
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollResult = 0;
		if (numberOfSides <= 1) {
			sendMessage.setText("¿Un dado con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = 0;
			do {
				valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				rollResult += valor;
			} while (valor == numberOfSides);
			// Collections.sort(dice);
			sendMessage.setText("*[d" + numberOfSides + "!]*-> [" + dice + "] = *" + rollResult + "*");
			sendMessage.setParseMode("Markdown");
		}
		answerUser();
	}

	private void simpleRollWithModifier(String command) {
		// d5+5
		String[] numericParts = command.split("\\D+");
		char modifier = '+';
		if (command.contains("-")) {
			modifier = '-';
		}
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollModifier = Integer.parseInt(numericParts[2]);
		rollResult = 0;
		if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = (int) (Math.random() * numberOfSides + 1);
			dice.add(valor);
			rollResult += valor;
			Collections.sort(dice);
			if (modifier == '+') {
				sendMessage.setText("*[d" + numberOfSides + "" + modifier + rollModifier + "]*->[" + dice + "] + "
						+ rollModifier + " = *" + (rollResult + rollModifier) + "*");
			} else {
				sendMessage.setText("*[d" + numberOfSides + "" + modifier + rollModifier + "]*->[" + dice + "] - "
						+ rollModifier + " = *" + (rollResult - rollModifier) + "*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void simpleExplosiveRollWithModifier(String command) {
		// d5!+5
		String[] numericParts = command.split("\\D+");
		char modifier = '+';
		if (command.contains("-")) {
			modifier = '-';
		}
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollModifier = Integer.parseInt(numericParts[2]);
		rollResult = 0;
		if (numberOfSides <= 1) {
			sendMessage.setText("¿Que dado conoces menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = 0;
			do {
				valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				rollResult += valor;
			} while (valor == numberOfSides);
			// Collections.sort(dice);
			if (modifier == '+') {
				sendMessage.setText("*[d" + numberOfSides + "!" + modifier + rollModifier + "]*->[" + dice + "] + "
						+ rollModifier + " = *" + (rollResult + rollModifier) + "*");
			} else {
				sendMessage.setText("*[d" + numberOfSides + "!" + modifier + rollModifier + "]*->[" + dice + "] - "
						+ rollModifier + " = *" + (rollResult - rollModifier) + "*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void severalDice(String command) {
		// 3d6
		String[] numericParts = command.split("d");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollResult = 0;

		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			for (int i = 0; i < numberOfDice; i++) {
				int valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				rollResult += valor;
			}
			Collections.sort(dice);
			sendMessage
					.setText("*[" + numberOfDice + "d" + numberOfSides + "]*-> [" + dice + "] = *" + rollResult + "*");
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void severalExplosiveDice(String command) {
		// 3d6!
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollResult = 0;

		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = 0;
			for (int i = 0; i < numberOfDice; i++) {
				do {
					valor = (int) (Math.random() * numberOfSides + 1);
					dice.add(valor);
					rollResult += valor;
				} while (valor == numberOfSides);
			}
			// Collections.sort(dice);
			sendMessage
					.setText("*[" + numberOfDice + "d" + numberOfSides + "!]*-> [" + dice + "] = *" + rollResult + "*");
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void severalDiceWithModifier(String command) {
		// 3d6+10 2d8-7
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollModifier = Integer.parseInt(numericParts[2]);
		rollResult = 0;
		char modifier = '+';
		if (command.contains("-")) {
			modifier = '-';
		}
		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			for (int i = 0; i < numberOfDice; i++) {
				int valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				rollResult += valor;
			}
			Collections.sort(dice);
			if (modifier == '+') {
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "" + modifier + rollModifier + "]*-> ["
						+ dice + "] + " + rollModifier + " = *" + (rollResult + rollModifier) + "*");
			} else if (modifier == '-') {
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "" + modifier + rollModifier + "]*-> ["
						+ dice + "] - " + rollModifier + " = *" + (rollResult - rollModifier) + "*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void severalExplosiveDiceWithModifier(String command) {
		// 3d6!+5
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollModifier = Integer.parseInt(numericParts[2]);
		rollResult = 0;
		char modifier = '+';
		if (command.contains("-")) {
			modifier = '-';
		}
		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Que dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = 0;
			for (int i = 0; i < numberOfDice; i++) {
				do {
					valor = (int) (Math.random() * numberOfSides + 1);
					dice.add(valor);
					rollResult += valor;
				} while (valor == numberOfSides);
			}
			// Collections.sort(dice);
			if (modifier == '+') {
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!" + modifier + rollModifier + "]*-> ["
						+ dice + "] + " + rollModifier + " = *" + (rollResult + rollModifier) + "*");
			} else if (modifier == '-') {
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!" + modifier + rollModifier + "]*-> ["
						+ dice + "] - " + rollModifier + " = *" + (rollResult - rollModifier) + "*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void severalKeepRoll(String command) {
		// 3d6h1
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		diceToKeep = Integer.parseInt(numericParts[2]);
		rollResult = 0;

		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			for (int i = 0; i < numberOfDice; i++) {
				int valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				rollResult += valor;
			}
			Collections.sort(dice);
			if (diceToKeep > numberOfDice) {
				sendMessage.setText("La tirada no es de tantos dados");
			} else if (diceToKeep <= 0) {
				sendMessage.setText("No puedes quedarte con ningún dado");
			} else if (diceToKeep == numberOfDice) {
				sendMessage.setText(
						"*[" + numberOfDice + "d" + numberOfSides + "]*-> [" + dice + "] = *" + rollResult + "*");
			} else if (command.contains("h") || command.contains("hl")) {
				rollResult = 0;
				List<Integer> diceThrown = new ArrayList<Integer>(dice.subList(0, dice.size() - diceToKeep));
				List<Integer> diceKept = dice.subList(dice.size() - diceToKeep, dice.size());
				for (Integer value : diceKept) {
					rollResult += value;
				}
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "h" + diceToKeep + "]*->_" + diceThrown
						+ "_ [" + diceKept + "] = *" + rollResult + "*");
			} else {
				rollResult = 0;
				List<Integer> diceKept = dice.subList(0, diceToKeep);
				List<Integer> diceThrown = dice.subList(diceToKeep, dice.size());

				for (Integer value : diceKept) {
					rollResult += value;
				}
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "l" + diceToKeep + "]*->*" + diceKept
						+ "*_ " + diceThrown + "_ = *" + rollResult + "*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void severalExplosiveKeepRoll(String command) {
		// 3d6!h1
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		diceToKeep = Integer.parseInt(numericParts[2]);
		rollResult = 0;

		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = 0;
			for (int i = 0; i < numberOfDice; i++) {
				do {
					valor = (int) (Math.random() * numberOfSides + 1);
					dice.add(valor);
					rollResult += valor;
				} while (valor == numberOfSides);
			}
			Collections.sort(dice);
			if (diceToKeep > numberOfDice) {
				sendMessage.setText("La tirada no es de tantos dados");
			} else if (diceToKeep <= 0) {
				sendMessage.setText("No puedes quedarte con ningún dado");
			} else if (command.contains("h") || command.contains("hl")) {
				rollResult = 0;
				List<Integer> diceThrown = new ArrayList<Integer>(dice.subList(0, dice.size() - diceToKeep));
				List<Integer> diceKept = dice.subList(dice.size() - diceToKeep, dice.size());
				for (Integer value : diceKept) {
					rollResult += value;
				}
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!h" + diceToKeep + "]*->_" + diceThrown
						+ "_ *" + diceKept + "* = *" + rollResult + "*");
			} else {
				rollResult = 0;
				List<Integer> diceKept = dice.subList(0, diceToKeep);
				List<Integer> diceThrown = dice.subList(diceToKeep, dice.size());

				for (Integer value : diceKept) {
					rollResult += value;
				}
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!l" + diceToKeep + "]*->*" + diceKept
						+ "*_ " + diceThrown + "_ = *" + rollResult + "*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();

	}

	private void severalKeepRollWithModifier(String command) {
		// 10d6h3+5
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		diceToKeep = Integer.parseInt(numericParts[2]);
		rollModifier = Integer.parseInt(numericParts[3]);
		char modifier = '+';
		if (command.contains("-")) {
			modifier = '-';
		}
		rollResult = 0;
		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			for (int i = 0; i < numberOfDice; i++) {
				int valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				rollResult += valor;
			}
			Collections.sort(dice);
			if (diceToKeep > numberOfDice) {
				sendMessage.setText("La tirada no es de tantos dados");
			} else if (diceToKeep <= 0) {
				sendMessage.setText("No puedes quedarte con ningún dado");
			} else if (diceToKeep == numberOfDice) {
				if (modifier == '+') {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + rollModifier + "]*-> [" + dice
							+ "] + " + rollModifier + " = *" + (rollResult + rollModifier) + "*");
				} else {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + rollModifier + "]*-> [" + dice
							+ "] - " + rollModifier + " = *" + (rollResult - rollModifier) + "*");
				}
			} else if (command.contains("h") || command.contains("hl")) {
				rollResult = 0;
				List<Integer> diceThrown = new ArrayList<Integer>(dice.subList(0, dice.size() - diceToKeep));
				List<Integer> diceKept = dice.subList(dice.size() - diceToKeep, dice.size());
				for (Integer value : diceKept) {
					rollResult += value;
				}
				if (modifier == '+') {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "h" + diceToKeep + "+"
							+ rollModifier + "]*-> _" + diceThrown + "_ *" + diceKept + "* +" + rollModifier + " = *"
							+ (rollResult + rollModifier) + "*");
				} else {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "h" + diceToKeep + "-"
							+ rollModifier + "]*-> _" + diceThrown + "_ *" + diceKept + "* -" + rollModifier + " = *"
							+ (rollResult - rollModifier) + "*");
				}
			} else {
				rollResult = 0;
				List<Integer> diceKept = dice.subList(0, diceToKeep);
				List<Integer> diceThrown = dice.subList(diceToKeep, dice.size());

				for (Integer value : diceKept) {
					rollResult += value;
				}
				if (modifier == '+') {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "l" + diceToKeep + "+"
							+ rollModifier + "]*-> *" + diceKept + "* _" + diceThrown + "_ +" + rollModifier + " = *"
							+ (rollResult + rollModifier) + "*");
				} else {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "l" + diceToKeep + "-"
							+ rollModifier + "]*-> *" + diceKept + "* _" + diceThrown + "_ -" + rollModifier + " = *"
							+ (rollResult - rollModifier) + "*");
				}
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void severalExplosiveKeepRollWithModifier(String command) {
		// 10d6h3+5
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[0]);
		numberOfSides = Integer.parseInt(numericParts[1]);
		diceToKeep = Integer.parseInt(numericParts[2]);
		rollModifier = Integer.parseInt(numericParts[3]);
		char modifier = '+';
		if (command.contains("-")) {
			modifier = '-';
		}
		rollResult = 0;
		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (numberOfSides <= 1) {
			sendMessage.setText("¿Qué dado conoces con menos de dos caras?");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			int valor = 0;
			for (int i = 0; i < numberOfDice; i++) {
				do {
					valor = (int) (Math.random() * numberOfSides + 1);
					dice.add(valor);
					rollResult += valor;
				} while (valor == numberOfSides);
			}
			Collections.sort(dice);
			if (diceToKeep > numberOfDice) {
				sendMessage.setText("La tirada no es de tantos dados");
			} else if (diceToKeep <= 0) {
				sendMessage.setText("No puedes quedarte con ningún dado");
			} else if (diceToKeep == numberOfDice) {
				if (modifier == '+') {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + rollModifier + "]*-> [" + dice
							+ "] + " + rollModifier + " = *" + (rollResult + rollModifier) + "*");
				} else {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + rollModifier + "]*-> [" + dice
							+ "] - " + rollModifier + " = *" + (rollResult - rollModifier) + "*");
				}
			} else if (command.contains("h") || command.contains("hl")) {
				rollResult = 0;
				List<Integer> diceThrown = new ArrayList<Integer>(dice.subList(0, dice.size() - diceToKeep));
				List<Integer> diceKept = dice.subList(dice.size() - diceToKeep, dice.size());
				for (Integer value : diceKept) {
					rollResult += value;
				}
				if (modifier == '+') {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!h" + diceToKeep + "+"
							+ rollModifier + "]*-> _" + diceThrown + "_ *" + diceKept + "* +" + rollModifier + " = *"
							+ (rollResult + rollModifier) + "*");
				} else {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!h" + diceToKeep + "-"
							+ rollModifier + "]*-> _" + diceThrown + "_ *" + diceKept + "* -" + rollModifier + " = *"
							+ (rollResult - rollModifier) + "*");
				}
			} else {
				rollResult = 0;
				List<Integer> diceKept = dice.subList(0, diceToKeep);
				List<Integer> diceThrown = dice.subList(diceToKeep, dice.size());

				for (Integer value : diceKept) {
					rollResult += value;
				}
				if (modifier == '+') {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!l" + diceToKeep + "+"
							+ rollModifier + "]*-> *" + diceKept + "* _" + diceThrown + "_ +" + rollModifier + " = *"
							+ (rollResult + rollModifier) + "*");
				} else {
					sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + "!l" + diceToKeep + "-"
							+ rollModifier + "]*-> *" + diceKept + "* _" + diceThrown + "_ -" + rollModifier + " = *"
							+ (rollResult - rollModifier) + "*");
				}
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void savageWorldRoll(String command) {
		// "(s|savage)(4|6|8|10|12)"
		// roll {1d6+1d8}kh1
		String[] numericParts = command.split("\\D+");
		numberOfSides = Integer.parseInt(numericParts[1]);
		rollResult = 0;

		ArrayList<Integer> wildDice = new ArrayList<Integer>();
		int wild = 0, wildResult = 0;
		do {
			wild = (int) (Math.random() * 6 + 1);
			wildDice.add(wild);
			wildResult += wild;
			System.out.println("wild" + wild);
		} while (wild == 6);

		ArrayList<Integer> dice = new ArrayList<Integer>();
		int value = 0;
		do {
			value = (int) (Math.random() * numberOfSides + 1);
			dice.add(value);
			rollResult += value;
			System.out.println("roll" + value);
		} while (value == numberOfSides);
		int finalResult = (wildResult > rollResult) ? wildResult : rollResult;
		int increases = 1;
		while ((finalResult - increases * 4) - 4 >= 0) {
			increases++;
		}
		if (command.contains("-")) {
			sendMessage.setText("*Savage Worlds [6] [4] -2*\nSalvaje: [" + wildDice + "] -2 = " + (wildResult - 2)
					+ "\nHabilidad: [" + dice + "] -2 = " + (rollResult - 2) + "\n*Total: " + (finalResult - 2) + " -> "
					+ (increases - 1) + " aumento/s*");
		} else {
			sendMessage.setText("*Savage Worlds [6] [" + numberOfSides + "]*\nSalvaje: [" + wildDice + "] = "
					+ wildResult + "\nHabilidad: [" + dice + "] = " + rollResult + "\n*Total: " + finalResult + " -> "
					+ (increases - 1) + " aumento/s*");
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void inNomineSatanisRoll(String command) {
		// 3d6
		rollResult = 0;
		numberOfDice = 3;
		numberOfSides = 6;
		String roll = "";
		for (int i = 0; i < numberOfDice; i++) {
			int valor = (int) (Math.random() * numberOfSides + 1);
			roll += valor;
		}
		if (roll == "111") {
			sendMessage.setText("*[In Nomine Satanis]* = *" + roll + "*");
			sendVideo.setVideo("https://media.giphy.com/media/l2QE7PACf4cJccwA8/giphy.gif");
			try {
				execute(sendVideo);
			} catch (TelegramApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (roll == "666") {
			sendMessage.setText("*[In Nomine Satanis]* = *" + roll + "*");
			sendVideo.setVideo("https://media.giphy.com/media/hB5vNhUepvcek/giphy.mp4");
			try {
				execute(sendVideo);
			} catch (TelegramApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			sendMessage.setText("*[In Nomine Satanis]* = " + roll);
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
		sendPhoto.setPhoto("https://i.imgur.com/PWsDzmW.jpg");
		try {
			execute(sendVideo);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void vampireRoll(String command) {
		// v7
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[1]);
		int difficulty = 6;
		numberOfSides = 10;
		int success = 0;
		int ones = 0;

		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			for (int i = 0; i < numberOfDice; i++) {
				int valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				if (valor == 1) {
					ones++;
				}
				if (valor >= difficulty) {
					success++;
				}
			}
			Collections.sort(dice);
			if (success > ones) {
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + "]*-> [" + dice
						+ "] = *" + (success - ones) + " éxito/s*");
			} else if (success < ones) {
				sendMessage.setText(
						"*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + "]*-> [" + dice + "] = *Pífia*");
			} else {
				sendMessage.setText(
						"*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + "]*-> [" + dice + "] = *Fallo*");
			}
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void vampireRollWithCustomDificulty(String command) {
		// v6d3
		String[] numericParts = command.split("\\D+");
		numberOfDice = Integer.parseInt(numericParts[1]);
		int difficulty = Integer.parseInt(numericParts[2]);
		if (difficulty < 2 || difficulty > 10) {
			difficulty = Integer.parseInt(numericParts[2]);
		}
		numberOfSides = 10;
		int success = 0;
		int ones = 0;
		if ((numberOfDice <= 0) || (numberOfDice > 50)) {
			sendMessage.setText("No puedo tirar esa cantidad de dados. Lanza al menos un dado y un máximo de 50");
		} else if (difficulty < 2 || difficulty > 10) {
			sendMessage.setText("La dificultad ha de ser mínimo de 2 y máximo de 10");
		} else {
			ArrayList<Integer> dice = new ArrayList<Integer>();
			for (int i = 0; i < numberOfDice; i++) {
				int valor = (int) (Math.random() * numberOfSides + 1);
				dice.add(valor);
				if (valor == 1) {
					ones++;
				}
				if (valor >= difficulty) {
					success++;
				}
			}
			Collections.sort(dice);
			if (success > ones) {
				sendMessage.setText("*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + "]*-> [" + dice
						+ "] = *" + (success - ones) + " éxito/s*");
			} else if (success < ones) {
				sendMessage.setText(
						"*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + "]*-> [" + dice + "] = *Pífia*");
			} else {
				sendMessage.setText(
						"*[" + numberOfDice + "d" + numberOfSides + ">" + difficulty + "]*-> [" + dice + "] = *Fallo*");
			}

		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void fateRoll(String command) {
		numberOfDice = 4;
		numberOfSides = 6;
		rollResult = 0;
		ArrayList<Integer> dice = new ArrayList<Integer>();
		for (int i = 0; i < numberOfDice; i++) {
			int valor = (int) (Math.random() * numberOfSides + 1);
			dice.add(valor);
		}
		sendMessage.setText("*[FATE]*-> ");
		for (Integer dado : dice) {
			if (dado == 1 || dado == 2) {
				sendMessage.setText(sendMessage.getText() + "➖");
				rollResult -= 1;
			} else if (dado == 3 || dado == 4) {
				sendMessage.setText(sendMessage.getText() + "⭕");
			} else if (dado == 5 || dado == 6) {
				sendMessage.setText(sendMessage.getText() + "➕");
				rollResult += 1;
			}
		}
		sendMessage.setText(sendMessage.getText() + " = *" + rollResult + "*");
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void fateRollWithModifier(String command) {
		// fate+5 f-7
		String[] numericParts = command.split("\\D+");
		rollModifier = Integer.parseInt(numericParts[1]);
		numberOfDice = 4;
		numberOfSides = 6;
		rollResult = 0;
		char modifier = '+';
		if (command.contains("-")) {
			modifier = '-';
		}
		ArrayList<Integer> dice = new ArrayList<Integer>();
		for (int i = 0; i < numberOfDice; i++) {
			int valor = (int) (Math.random() * numberOfSides + 1);
			dice.add(valor);
		}
		sendMessage.setText("*[FATE]*-> ");
		for (Integer dado : dice) {
			if (dado == 1 || dado == 2) {
				sendMessage.setText(sendMessage.getText() + "➖");
				rollResult -= 1;
			} else if (dado == 3 || dado == 4) {
				sendMessage.setText(sendMessage.getText() + "⭕");
			} else if (dado == 5 || dado == 6) {
				sendMessage.setText(sendMessage.getText() + "➕");
				rollResult += 1;
			}
		}
		if (modifier == '+') {
			sendMessage
					.setText(sendMessage.getText() + "+" + rollModifier + " = *" + (rollResult + rollModifier) + "*");
		} else {
			sendMessage
					.setText(sendMessage.getText() + "-" + rollModifier + " = *" + (rollResult - rollModifier) + "*");
		}
		// sendMessage.setText(sendMessage.getText() + " *" + rollResult + "*");
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void hitosRoll(String command) {
		// 3d10
		numberOfDice = 3;
		numberOfSides = 10;
		rollResult = 0;

		ArrayList<Integer> dice = new ArrayList<Integer>();
		for (int i = 0; i < numberOfDice; i++) {
			int valor = (int) (Math.random() * numberOfSides + 1);
			dice.add(valor);
			rollResult += valor;
		}
		Collections.sort(dice);
		if ((dice.get(0) == 1) && (dice.get(1) == 1)) {
			if (dice.get(2) == 1) {
				sendMessage.setText(
						"*[Hitos]*-> [" + dice.get(0) + "," + dice.get(1) + "," + dice.get(2) + "] -> * Pífia Triple*");
			} else {
				sendMessage.setText(
						"*[Hitos]*-> [" + dice.get(0) + "," + dice.get(1) + "," + dice.get(2) + "] -> * Pífia Doble*");
			}
		} else if ((dice.get(0) == 10) && (dice.get(1) == 10)) {
			if (dice.get(2) == 10) {
				sendMessage.setText(
						"*[Hitos]*-> [" + dice.get(0) + "," + dice.get(1) + "," + dice.get(2) + "] -> * Éxito Triple*");
			} else {
				sendMessage.setText(
						"*[Hitos]*-> [" + dice.get(0) + "," + dice.get(1) + "," + dice.get(2) + "] -> * Éxito Doble*");
			}
		} else {
			sendMessage.setText("*[Hitos]*-> [" + dice.get(0) + "," + dice.get(1) + "," + dice.get(2) + "] = *"
					+ dice.get(1) + "*");
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void hitosRollWithModifier(String command) {
		// 3d10+7 3d10-62
		String[] numericParts = command.split("\\D+");
		numberOfDice = 3;
		numberOfSides = 10;
		rollResult = 0;
		rollModifier = Integer.parseInt(numericParts[1]);
		ArrayList<Integer> dice = new ArrayList<Integer>();
		for (int i = 0; i < numberOfDice; i++) {
			int valor = (int) (Math.random() * numberOfSides + 1);
			dice.add(valor);
			rollResult += valor;
		}
		Collections.sort(dice);
		if ((dice.get(0) == 1) && (dice.get(1) == 1)) {
			if (dice.get(2) == 1) {
				sendMessage.setText("*[Hitos + " + rollModifier + "]*-> [" + dice.get(0) + "," + dice.get(1) + ","
						+ dice.get(2) + "] -> * Pífia Triple*");
			} else {
				sendMessage.setText("*[Hitos + " + rollModifier + "]*-> [" + dice.get(0) + "," + dice.get(1) + ","
						+ dice.get(2) + "] -> * Pífia Doble*");
			}
		} else if ((dice.get(1) == 10) && (dice.get(2) == 10)) {
			if (dice.get(0) == 10) {
				sendMessage.setText("*[Hitos + " + rollModifier + "]*-> [" + dice.get(0) + "," + dice.get(1) + ","
						+ dice.get(2) + "] -> * Éxito Triple*");
			} else {
				sendMessage.setText("*[Hitos + " + rollModifier + "]*-> [" + dice.get(0) + "," + dice.get(1) + ","
						+ dice.get(2) + "] -> * Éxito Doble*");
			}
		} else {
			sendMessage.setText("*[Hitos + " + rollModifier + "]*-> [" + dice.get(0) + "," + dice.get(1) + ","
					+ dice.get(2) + "] = *" + (dice.get(1) + rollModifier) + "*");
		}
		sendMessage.setParseMode("Markdown");
		answerUser();

	}

	private void PoweredByTheApocapilse(String command) {
		// pbta p
		String[] numericParts = command.split("\\D+");
		numberOfDice = 2;
		numberOfSides = 6;
		rollResult = 0;
		ArrayList<Integer> dice = new ArrayList<Integer>();
		for (int i = 0; i < numberOfDice; i++) {
			int valor = (int) (Math.random() * numberOfSides + 1);
			dice.add(valor);
			rollResult += valor;
		}
		Collections.sort(dice);
		String result = "";
		if (rollResult < 6) {
			result = "FALLO";
		} else if (rollResult < 10) {
			result = "ÉXITO PARCIAL";
		} else {
			result = "ÉXITO";
		}

		sendMessage.setText("*[PbtA]* *[" + numberOfDice + "d" + numberOfSides + "]*-> [" + dice + "] = *" + rollResult
				+ "->" + result + "*");

		sendMessage.setParseMode("Markdown");
		answerUser();
	}

	private void PoweredByTheApocapilseWithModifier(String command) {
		// pbta+1 p-1
		String[] numericParts = command.split("\\D+");
		numberOfDice = 2;
		numberOfSides = 6;
		rollModifier = Integer.parseInt(numericParts[1]);
		rollResult = 0;
		char modifier = '+';
		if (command.contains("-")) {
			modifier = '-';
		}
		ArrayList<Integer> dice = new ArrayList<Integer>();
		for (int i = 0; i < numberOfDice; i++) {
			int valor = (int) (Math.random() * numberOfSides + 1);
			dice.add(valor);
			rollResult += valor;
		}
		/*
		 * ah, es que si el resultado es <6 es fallo entre 7-9 es exito parcial >=10 es
		 * exito
		 */
		Collections.sort(dice);
		String result = "";
		if (modifier == '+') {
			if (rollResult + rollModifier < 6) {
				result = "FALLO";
			} else if (rollResult + rollModifier < 10) {
				result = "ÉXITO PARCIAL";
			} else {
				result = "ÉXITO";
			}
		} else {
			if (rollResult - rollModifier < 6) {
				result = "FALLO";
			} else if (rollResult - rollModifier < 10) {
				result = "ÉXITO PARCIAL";
			} else {
				result = "ÉXITO";
			}
		}
		if (modifier == '+') {
			sendMessage.setText(
					"*[PbtA]* *[" + numberOfDice + "d" + numberOfSides + "" + modifier + rollModifier + "]*-> [" + dice
							+ "] + " + rollModifier + " = *" + (rollResult + rollModifier) + "->" + result + "*");
		} else if (modifier == '-') {
			sendMessage.setText(
					"*[PbtA]* *[" + numberOfDice + "d" + numberOfSides + "" + modifier + rollModifier + "]*-> [" + dice
							+ "] - " + rollModifier + " = *" + (rollResult - rollModifier) + "->" + result + "*");
		}
		sendMessage.setParseMode("Markdown");
		answerUser();
	}
}
