import entities.Elemento;
import entities.Libro;
import jakarta.persistence.*;

import java.util.List;

public class Main {

    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("catalogo_bibliografico");

    static EntityManager em = emf.createEntityManager();

    public static void main(String[] args) {
        Libro nuovoLibro = new Libro();
        nuovoLibro.setTitolo("Il Signore degli Anelli");
        nuovoLibro.setAutore("J.R.R. Tolkien");
        nuovoLibro.setAnnoPubblicazione(1954);
        nuovoLibro.setIsbn("1234567890");
        nuovoLibro.setNumeroPagine(399);

        aggiungiLibro(nuovoLibro);

        try {
            rimuoviElementoPerIsbn("1234567890");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    private static void aggiungiLibro(Libro libro) {
        try {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();

            em.persist(libro);

            transaction.commit();

            System.out.println("Il libro è stato salvato correttamente!");
        }
        finally {
            // em.close();
        }

    }

    private static void rimuoviElementoPerIsbn(String isbn) throws Exception {
        String jpql = "SELECT l FROM Libro l WHERE l.isbn = :isbn";

        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        Libro libro;

        try {
            libro = em.createQuery(jpql, Libro.class)
                    .setParameter("isbn", isbn)
                    .getSingleResult();
        }
        catch(RuntimeException e) {
            throw new Exception("Libro con ISBN " + isbn + " non trovato!");
        }

        if (libro != null) {
            em.remove(libro);
        }

        transaction.commit();
        em.close();
    }

    private Libro cercaPerIsbn(String isbn) {
        String jpql = "SELECT l FROM Libro l WHERE l.isbn = :isbn";
        try {
            return em.createQuery(jpql, Libro.class)
                    .setParameter("isbn", isbn)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private List<Elemento> cercaPerAnno(int anno) {
        String jpql = "SELECT e FROM Elemento e WHERE e.annoPubblicazione = :anno";
        return em.createQuery(jpql, Elemento.class)
                .setParameter("anno", anno)
                .getResultList();
    }

    private List<Elemento> cercaPerAutore(String autore) {
        String jpql = "SELECT e FROM Elemento e WHERE e.autore = :autore";
        return em.createQuery(jpql, Elemento.class)
                .setParameter("autore", autore)
                .getResultList();
    }

    public List<Elemento> cercaPerTitolo(String titolo) {
        String jpql = "SELECT e FROM Elemento e WHERE LOWER(e.titolo) LIKE LOWER(:titolo)";
        return em.createQuery(jpql, Elemento.class)
                .setParameter("titolo", "%" + titolo + "%")
                .getResultList();
    }

    private List<Elemento> cercaElementiInPrestitoPerUtente(Long numeroTessera) {
        String jpql = "SELECT p.elemento FROM Prestito p WHERE p.utente.numeroTessera = :numeroTessera AND p.dataRestituzioneEffettiva IS NULL";
        return em.createQuery(jpql, Elemento.class)
                .setParameter("numeroTessera", numeroTessera)
                .getResultList();
    }






}
