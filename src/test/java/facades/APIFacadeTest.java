package facades;

import dtos.FullPersonDTO;
import dtos.PersonDTO;
import dtos.ZipcodesDTO;
import entities.*;
import org.junit.jupiter.api.*;
import utils.EMF_Creator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;

class APIFacadeTest {
    private static EntityManagerFactory emf;
    private static APIFacade facade;
    private Person p1, p2, p3;
    private PersonDTO p1DTO, p2DTO, p3DTO;
    private FullPersonDTO fp3DTO;
    private Hobby h1, h2, h3;
    private Address a1, a2, a3;
    private Phone ph1, ph2, ph3;

    @BeforeAll
    public static void setUpClass()
    {
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        facade = APIFacade.getInstance(emf);
    }

    @BeforeEach
    void setUp() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        em.createQuery("delete from Phone").executeUpdate();
        em.createQuery("delete from Person").executeUpdate();
        em.createQuery("delete from Address").executeUpdate();


        p1 = new Person("testemail1", "Test", "Person1");
        p2 = new Person("testemail2", "Test", "Person2");
        p3 = new Person("testemail3", "Test", "Person3");

        a1 = new Address("some street", "th", em.find(CityInfo.class, "3720"));
        ph1 = new Phone("12345678", "hjemmetelefon");
        h1 = em.find(Hobby.class, 1L);
        h2 = em.find(Hobby.class, 2L);
        h3 = em.find(Hobby.class, 3L);

        p3.setLastName("Lname");
        p3.setAddress(a1);
        p2.setAddress(a1);
        p3.addHobbytoHobbySet(h1);
        p3.addPhone(ph1);

        p1.addHobbytoHobbySet(h1);
        p2.addHobbytoHobbySet(h2);

        em.persist(a1);
        em.persist(ph1);
        em.persist(p1);
        em.persist(p2);
        em.persist(p3);


        em.getTransaction().commit();
        em.close();

        p1DTO = new PersonDTO(p1);
        p2DTO = new PersonDTO(p2);
        p3DTO = new PersonDTO(p3);
        fp3DTO = new FullPersonDTO(p3);

    }

    @Test
    void getPersonByPhone() {
        FullPersonDTO actual = facade.getPersonByPhone(p3.getPhone().iterator().next().getNumber());
        assertEquals(fp3DTO, actual);
    }

    @Test
    void getPersonByHobby() {
        List<PersonDTO> actual = facade.getPersonsByHobby(p1.getHobbies().iterator().next().getName());
//        PersonDTO expected = p1DTO;
        assertThat(actual, containsInAnyOrder(p1DTO, p3DTO));

//        assertThat(actual, containsInAnyOrder(e1DTO, e2DTO, e3DTO, employeeDTO));
    }

    @Test
    void getAllFromCity() {
        List<PersonDTO> actual = facade.getAllFromCity(a1.getCityInfo().getZipCode());
        assertThat(actual, containsInAnyOrder(p3DTO, p2DTO));
    }

    @Test
    void getNumberWithHobby() {
        int actual1 = facade.getNumberWithHobby(h1.getName());
        int actual2 = facade.getNumberWithHobby(h2.getName());

        assertEquals(2, actual1);
        assertEquals(1, actual2);
    }

    @Test
    void getAllZipcodes() {
        ZipcodesDTO actual = facade.getAllZipcodes();
        assertThat(actual.getAll(), hasItems("3720", "0960", "470", "186", "5800"));
    }


    @Test
    void createPerson() {
        EntityManager em = emf.createEntityManager();
        Person newPerson = new Person("testMail", "fName", "lName");
        Phone newPhone = new Phone("87654321", "testPhone");
        Address newAddress = new Address("new street", "up", em.find(CityInfo.class, "3720"));
        newPerson.addPhone(newPhone);
        newPerson.setAddress(newAddress);
        newPerson = facade.createPerson(newPerson);
        FullPersonDTO actual = facade.getPersonByPhone(newPhone.getNumber());
        FullPersonDTO expected = new FullPersonDTO(newPerson);

        assertEquals(expected, actual);
    }

}