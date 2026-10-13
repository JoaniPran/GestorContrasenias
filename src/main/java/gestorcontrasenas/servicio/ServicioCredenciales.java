package gestorcontrasenas.servicio;

import gestorcontrasenas.modelo.RegistroContrasena;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class ServicioCredenciales {

	public boolean existeDuplicado(List<RegistroContrasena> registros, RegistroContrasena candidato,
			int indiceIgnorado) {
		Objects.requireNonNull(registros, "registros");
		Objects.requireNonNull(candidato, "candidato");
		for (int i = 0; i < registros.size(); i++) {
			if (i == indiceIgnorado) {
				continue;
			}
			RegistroContrasena existente = registros.get(i);
			if (normalizar(existente.getCuenta()).equals(normalizar(candidato.getCuenta()))
					&& normalizar(existente.getUsuario()).equals(normalizar(candidato.getUsuario()))) {
				return true;
			}
		}
		return false;
	}

	public ResultadoImportacion combinarImportacion(List<RegistroContrasena> actuales,
			List<RegistroContrasena> importados) {
		Objects.requireNonNull(actuales, "actuales");
		Objects.requireNonNull(importados, "importados");

		List<RegistroContrasena> combinados = new ArrayList<>(actuales);
		Map<String, Integer> indices = new HashMap<>();
		for (int i = 0; i < combinados.size(); i++) {
			indices.put(clave(combinados.get(i)), i);
		}

		int agregados = 0;
		int reemplazados = 0;
		for (RegistroContrasena importado : importados) {
			Objects.requireNonNull(importado, "registro importado");
			String clave = clave(importado);
			Integer indiceExistente = indices.get(clave);
			if (indiceExistente == null) {
				indices.put(clave, combinados.size());
				combinados.add(importado);
				agregados++;
			} else {
				RegistroContrasena existente = combinados.get(indiceExistente);
				combinados.set(indiceExistente, new RegistroContrasena(
						existente.getCuenta(), existente.getUsuario(), importado.getPassword()));
				reemplazados++;
			}
		}
		return new ResultadoImportacion(combinados, agregados, reemplazados);
	}

	private String clave(RegistroContrasena registro) {
		return normalizar(registro.getCuenta()) + '\u0000' + normalizar(registro.getUsuario());
	}

	private String normalizar(String valor) {
		return valor.trim().toLowerCase(Locale.ROOT);
	}

	public static final class ResultadoImportacion {
		private final List<RegistroContrasena> registros;
		private final int agregados;
		private final int reemplazados;

		private ResultadoImportacion(List<RegistroContrasena> registros, int agregados, int reemplazados) {
			this.registros = new ArrayList<>(registros);
			this.agregados = agregados;
			this.reemplazados = reemplazados;
		}

		public List<RegistroContrasena> getRegistros() {
			return new ArrayList<>(registros);
		}

		public int getAgregados() {
			return agregados;
		}

		public int getReemplazados() {
			return reemplazados;
		}
	}
}
