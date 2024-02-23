package org.lucasv.junitapp.ejemplos.models;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumingThat;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.lucasv.junitapp.ejemplos.exceptions.DineroInsuficienteException;

 class CuentaTest {
	Cuenta cuenta;
	private TestInfo testInfo;
	private TestReporter testReporter;
	@BeforeEach
	void initMetodoTest(TestInfo testInfo, TestReporter testReporter){
		this.cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
		this.testInfo = testInfo;
		this.testReporter = testReporter;
		
		System.out.println("Iniciando el método");
		testReporter.publishEntry("Ejecutando: " + testInfo.getDisplayName() + " " + testInfo.getTestMethod().orElse(null).getName() + " con las etiquetas " + testInfo.getTags());
	}
	
	@AfterEach
	void finMetodo(){
		System.out.println("Finalizando el metodo de prueba!");
	}

	@BeforeAll
	static void beforeAll(){
		System.out.println("Inicializando el Test");
	}
	
	@AfterAll
	static void afterAll(){
		System.out.println("Finalizando el Test");
	}

	@Tag("cuenta")
	@Nested
	@DisplayName("Probando Atributos de cuenta corriente")
	class CuentaTestNombreSaldo{
		@Test
		@DisplayName("Probando el nombre")
		void testNombreCuenta() {
			System.out.println(testInfo.getTags());
			if(testInfo.getTags().contains("cuenta")){
				System.out.println("etiqueta cuenta");
			}
			cuenta = new Cuenta("Andres", new BigDecimal("100.12345"));
			//cuenta.setPersona("Andres");
			String esperado = "Andres";
			String real = cuenta.getPersona();
			assertNotNull(real, () -> "La cuenta no puede ser nula");
			assertEquals(esperado, real, () -> "El nombre de la cuenta no es el que se esperaba");
			assertTrue(real.equals("Andres"), () -> "El nombre de la cuenta esperada debe ser igual a la real");
		}

		@Test
		@DisplayName("Probando el saldo")
		void testSaldoCuenta(){
			cuenta = new Cuenta("Andres", new BigDecimal("100.12345"));
			assertNotNull(cuenta.getSaldo());
			assertEquals(100.12345, cuenta.getSaldo().doubleValue());
			assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
			assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);

		}
		
		@Test
		@DisplayName("Testeando referencias que sean iguales con el método equals")
		void testRefernciaCuenta(){
			Cuenta cuenta = new Cuenta("John Doe", new BigDecimal("8900.177"));
			Cuenta cuenta2 = new Cuenta("John Doe", new BigDecimal("8900.177"));

			// assertNotEquals(cuenta2, cuenta);
			assertEquals(cuenta2, cuenta);
		}
	}

	@Nested
	class CuentaOperacionesTest{
		@Tag("cuenta")
		@Test
		void testDebitoCuenta(){
			cuenta.debito(new BigDecimal(100));
			assertNotNull(cuenta.getSaldo());
			assertEquals(900, cuenta.getSaldo().intValue());
			assertEquals("900.12345", cuenta.getSaldo().toPlainString());
		}

		@Tag("cuenta")
		@Test
		void testCreditoCuenta(){
			cuenta.credito(new BigDecimal(100));
			assertNotNull(cuenta.getSaldo());
			assertEquals(1100, cuenta.getSaldo().intValue());
			assertEquals("1100.12345", cuenta.getSaldo().toPlainString());
		}

		@Tag("cuenta")
		@Tag("banco")
		@Test
		void testTransferirDineroCuentas(){
			Cuenta cuenta1 = new Cuenta("Gerardo Dals", new BigDecimal("2500"));
			Cuenta cuenta2 = new Cuenta("Federico Spals", new BigDecimal("1500.1777"));

			Banco banco = new Banco();
			banco.setNombre("Banco del Estado");
			banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
			assertEquals("1000.1777",cuenta2.getSaldo().toPlainString());
			assertEquals("3000",cuenta1.getSaldo().toPlainString());
		}


	}
	
	@Tag("cuenta")
	@Tag("error")
	@Test
	void testDineroInsuficienteExceptionCuenta(){
		Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
			cuenta.debito(new BigDecimal(1500));
		});
		String actual = exception.getMessage();
		String esperado = "Dinero Insuficiente";
		assertEquals(esperado, actual);
	}

	

	@Test
	@DisplayName("Probando relaciones entre las cuentas y el banco con assertAll")
	void testRelacionBancoCuentas(){
		Cuenta cuenta1 = new Cuenta("Gerardo Dals", new BigDecimal("2500"));
		Cuenta cuenta2 = new Cuenta("Federico Spals", new BigDecimal("1500.1777"));

		Banco banco = new Banco();
		banco.addCuenta(cuenta1);
		banco.addCuenta(cuenta2);

		banco.setNombre("Banco del Estado");
		banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
		assertAll(() -> assertEquals("1000.1777",cuenta2.getSaldo().toPlainString()),
				() -> assertEquals("3000",cuenta1.getSaldo().toPlainString()),
				() -> assertEquals(2, banco.getCuentas().size()),
				() -> assertEquals("Banco del Estado", cuenta1.getBanco().getNombre()),
				() -> assertEquals("Federico Spals", banco.getCuentas().stream()
						.filter(c -> c.getPersona().equals("Federico Spals"))
						.findFirst()
						.get().getPersona()),
				() -> assertTrue(banco.getCuentas().stream()
						.filter(c -> c.getPersona().equals("Federico Spals"))
						.findFirst().isPresent()),
				() -> assertTrue(banco.getCuentas().stream()
						.anyMatch(c -> c.getPersona().equals("Gerardo Dals"))));
	}

	@Nested
	class SistemPropertiesTest{
		
		@Test
		void imprimirSystemProperties(){
			Properties properties = System.getProperties();
			properties.forEach((k,v) -> System.out.println(k + ":" + v));
		}

		@Test
		void testSaldoCuentadDev(){
			boolean esDev = "dev".equals(System.getProperty("ENV"));
			assumingThat(esDev, () -> {
				assertNotNull(cuenta.getSaldo());
				assertEquals(100.12345, cuenta.getSaldo().doubleValue());
				assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
				assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
			});
		}
	}
	
	@DisplayName("Probando Debito Cuenta Repetir")
	@RepeatedTest(value = 5, name = "{displayName} Repetición numero {currentRepetition} de {totalRepetitions}")
	void testDebitoCuentaRepetir(RepetitionInfo info){
		if(info.getCurrentRepetition()==3){
			System.out.println("Estamos en la repeticion " + info.getCurrentRepetition());
		}
		cuenta.debito(new BigDecimal(100));
		assertNotNull(cuenta.getSaldo());
		assertEquals(900, cuenta.getSaldo().intValue());
		assertEquals("900.12345", cuenta.getSaldo().toPlainString());
	}

	@Tag("param")
	@Nested
	class PruebasParametrizadas{
			
		@ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
		@ValueSource(strings = {"100", "200", "300", "500", "700", "1000"})
		void testDebitoCuentaValueSource(String monto){
			cuenta.debito(new BigDecimal(monto));
			assertNotNull(cuenta.getSaldo());
			assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
		}

		@ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
		@CsvSource({"1,100", "2,200", "3,300", "4,500", "5,700", "6,1000"})
		void testDebitoCuentaCsvSource(String index, String monto){
			System.out.println(index + "->" + monto);
			cuenta.debito(new BigDecimal(monto));
			assertNotNull(cuenta.getSaldo());
			assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
		}

		@ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
		@CsvFileSource(resources = "/data.csv")
		void testDebitoCuentaCsvFileSource(String monto){
			cuenta.debito(new BigDecimal(monto));
			assertNotNull(cuenta.getSaldo());
			assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
		}

		@ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
		@CsvSource({"200,100,John,Andres", "250,200,Pepe,PEpe", "300,300,Juan,Juan", "510,500,Sol,Sol", "750,700,Maria,Maria", "1000,1000,Esteban,Esteban"})
		void testDebitoCuentaCsvSource2(String saldo, String monto, String esperado, String actual){
			System.out.println(saldo + "->" + monto);
			cuenta.setSaldo(new BigDecimal(saldo));
			cuenta.debito(new BigDecimal(monto));
			cuenta.setPersona(actual);

			assertNotNull(saldo);
			assertNotNull(actual);
			assertEquals(esperado, actual);
			assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
		}

	}

	@Tag("param")
	@ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
	@MethodSource("montoList")
	void testDebitoCuentaMethodSource(String monto){
		cuenta.debito(new BigDecimal(monto));
		assertNotNull(cuenta.getSaldo());
		assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
	}

	static List<String> montoList(){
		return Arrays.asList("100", "200", "300", "500", "700", "1000");
	}

	@Nested
	class Timeouts{
		@Test
		@Timeout(5)
		void pruebaTimeout() throws InterruptedException{
			TimeUnit.SECONDS.sleep(4);
		}
	
		@Test
		void testTimeoutAssertions(){
			assertTimeout(Duration.ofSeconds(4), () -> {
				TimeUnit.MILLISECONDS.sleep(1000);
		});
	}
	}
	
}
