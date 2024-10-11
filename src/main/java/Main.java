import entities.Elemento;
import entities.Libro;
import jakarta.persistence.*;

import java.util.List;

public class Main {

    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("catalogo_bibliografico");
    static EntityManager em = emf.createEntityManager();

    public static void main(String[] args) {
        try {
            if (cercaPerIsbn("1234567890") == null) {
                Libro nuovoLibro = new Libro();
                nuovoLibro.setTitolo("Il Signore degli Anelli");
                nuovoLibro.setAutore("J.R.R. Tolkien");
                nuovoLibro.setAnnoPubblicazione(1954);
                nuovoLibro.setIsbn("1234567890");
                nuovoLibro.setNumeroPagine(399);

                aggiungiLibro(nuovoLibro);
            }

            List<Elemento> risultati = cercaPerAnno(1954);
            if (!risultati.isEmpty()) {
                System.out.println(risultati.get(0));
            } else {
                System.out.println("Nessun elemento trovato per l'anno specificato.");
            }
        } finally {
            em.close();
            emf.close();
        }
    }

    private static void aggiungiLibro(Libro libro) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(libro);
            transaction.commit();
            System.out.println("Il libro è stato salvato correttamente!");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    private static void rimuoviElementoPerIsbn(String isbn) throws Exception {
        String jpql = "SELECT l FROM Libro l WHERE l.isbn = :isbn";
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Libro libro = em.createQuery(jpql, Libro.class)
                    .setParameter("isbn", isbn)
                    .getSingleResult();
            em.remove(libro);
            transaction.commit();
            System.out.println("Libro rimosso correttamente.");
        } catch (NoResultException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new Exception("Libro con ISBN " + isbn + " non trovato!");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    private static Libro cercaPerIsbn(String isbn) {
        String jpql = "SELECT l FROM Libro l WHERE l.isbn = :isbn";
        try {
            return em.createQuery(jpql, Libro.class)
                    .setParameter("isbn", isbn)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private static List<Elemento> cercaPerAnno(int anno) {
        String jpql = "SELECT e FROM Elemento e WHERE e.annoPubblicazione = :anno";
        List<Elemento> risultati = em.createQuery(jpql, Elemento.class)
                .setParameter("anno", anno)
                .getResultList();
        return risultati;
    }


    private static List<Elemento> cercaPerAutore(String autore) {
        String jpql = "SELECT e FROM Elemento e WHERE e.autore = :autore";
        return em.createQuery(jpql, Elemento.class)
                .setParameter("autore", autore)
                .getResultList();
    }

    private static List<Elemento> cercaPerTitolo(String titolo) {
        String jpql = "SELECT e FROM Elemento e WHERE LOWER(e.titolo) LIKE LOWER(:titolo)";
        return em.createQuery(jpql, Elemento.class)
                .setParameter("titolo", "%" + titolo + "%")
                .getResultList();
    }

    private static List<Elemento> cercaElementiInPrestitoPerUtente(Long numeroTessera) {
        String jpql = "SELECT p.elemento FROM Prestito p WHERE p.utente.numeroTessera = :numeroTessera AND p.dataRestituzioneEffettiva IS NULL";
        return em.createQuery(jpql, Elemento.class)
                .setParameter("numeroTessera", numeroTessera)
                .getResultList();
    }
}
