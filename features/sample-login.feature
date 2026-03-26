@auth
Feature: Login
  Gestión de acceso con datos válidos e inválidos.

  Background:
    Given que estoy en la página de login

  Scenario: Acceso correcto
    When introduzco credenciales válidas
    Then accedo al sistema

  @negativo
  Scenario Outline: Acceso inválido
    Given que estoy en login
    When introduzco <usuario> y <password>
    Then veo el mensaje <mensaje>

    @smoke
    Examples: Casos básicos
      | usuario | password | mensaje |
      | a@a.com | 123      | Error   |
      |         |          | Vacío   |

    Examples: Casos extremos
      | usuario | password | mensaje |
      | root    | root     | Error   |
