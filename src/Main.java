import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String imagePath = "src/image.png"; //Ссылка на изображение-шаблон
        String message = "!Vladislav Lavrenko!"; //Сообщение
        String outputImagePath = "src/outputImage.png"; //Ссылка на изображение с сообщением

        Scanner scanner = new Scanner(System.in);

        hideMessage(imagePath, message, outputImagePath);

        String answer = extractMessage(outputImagePath);
        System.out.println("Extracted message: " + answer);

    }

    private static void hideMessage(String imagePath, String message, String outputImagePath){

        try {
            BufferedImage image = ImageIO.read(new File(imagePath)); //Считываем изображение
            int imageWidth = image.getWidth(); //Получаем длину и ширину
            int imageHeight = image.getHeight();
            int maxMessageLength = (imageWidth * imageHeight) / 8; // получаем максимально возможную длину сообщения
            if (message.length() + 8 > maxMessageLength) {  //Проверка на то, подойдет ли изображение для сокрытия или нет)
                System.out.println("Сообщение слишком длинное для данного изображения");
                return;
            }

            String messageLengthBinary = String.format("%32s", Integer.toBinaryString(message.length())).replace(' ', '0');
            int index = 0;

            for (int i = 0; i < 16; i++) { // В первые 16 пикселей меняем два младших бита
                int pixel = image.getRGB(index % imageWidth, index / imageWidth); // Получаем пиксель

                // Сбросим два младших бита
                pixel &= 0xFFFFFFFC;

                // Установим два младших бита в зависимости от сообщения
                pixel |= ((messageLengthBinary.charAt(2 * i) - '0') << 1) | (messageLengthBinary.charAt(2 * i + 1) - '0');

                // Устанавливаем пиксель в результат
                image.setRGB(index % imageWidth, index / imageWidth, pixel);

                index++; // Двигаемся дальше
            }


            // Скрытие сообщения в изображении
            byte[] messageBytes = message.getBytes(); //Преобразуем сообщение в последовательность байтов
            System.out.println(Arrays.toString(messageBytes)); //Вывод в консоль для проверки

            for (byte b : messageBytes) { // Проходим по байтовому представлению сообщения
                for (int j = 6; j >= 0; j -= 2) {
                    int pixel = image.getRGB(index % imageWidth, index / imageWidth);
                    int oldPixel = pixel;
                    pixel &= 0xFFFFFFFC; // Сбросим два младших бита
                    pixel |= ((b >> (6 - j)) & 3); // Установим два младших бита в зависимости от байта сообщения
                    image.setRGB(index % imageWidth, index / imageWidth, pixel);
                    index++;
                }
            }

            ImageIO.write(image, "png", new File(outputImagePath));
            System.out.println("Сообщение успешно скрыто в изображении");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extractMessage(String outputImagePath) {
        try {
            BufferedImage outputImage = ImageIO.read(new File(outputImagePath)); //Считываем изображение
            int imageWidth = outputImage.getWidth(); // Получаем длину изображения

            // Извлечение длины сообщения из изображения
            StringBuilder lengthBinary = new StringBuilder();
            int index = 0;

            for (int i = 0; i < 16; i++) { // Цикл для извлечения из двух младших бит в 16 пикселях
                int pixel = outputImage.getRGB(index % imageWidth, index / imageWidth);
                // Извлекаем два младших бита и добавляем их к строке
                for (int j = 1; j >= 0; j--) {
                    lengthBinary.append((pixel >> j) & 1);
                }
                index++;
            }

            int messageLength = Integer.parseInt(lengthBinary.toString(), 2); // Преобразуем строку в целочисленный тип

            // Извлечение сообщения из изображения (из двух младших бит)
            StringBuilder messageBinary = new StringBuilder();


            for (int i = 0; i < messageLength; i++) {
                for (int j = 3; j >= 0; j--) {
                    int pixel = outputImage.getRGB((index + j) % imageWidth, (index + j) / imageWidth);
                    messageBinary.append((pixel >> 1) & 1); // Извлекаем первый бит
                    messageBinary.append(pixel & 1);         // Извлекаем второй бит
                }
                index += 4;
            }


// Преобразование бинарного сообщения в строку
            StringBuilder extractedMessage = new StringBuilder();

            for (int i = 0; i < messageBinary.length(); i += 8) {
                extractedMessage.append((char) Integer.parseInt(messageBinary.substring(i, i + 8), 2));
            }

            byte[] messageBytes = extractedMessage.toString().getBytes();
            System.out.println(Arrays.toString(messageBytes));
            return extractedMessage.toString(); // Возвращаем сообщение

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}