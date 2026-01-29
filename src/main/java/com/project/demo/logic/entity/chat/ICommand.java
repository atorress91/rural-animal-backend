package com.project.demo.logic.entity.chat;

/**
 * Interfaz para definir comandos ejecutables en función de un mensaje de entrada.
 */
public interface ICommand {

    /**
     * Ejecuta el comando con el mensaje proporcionado.
     *
     * @param message el mensaje de entrada que se utilizará para la ejecución del comando.
     * @return el resultado de la ejecución del comando.
     */
    String execute(String message,Long userId);
}
