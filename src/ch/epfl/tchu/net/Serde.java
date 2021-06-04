package ch.epfl.tchu.net;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author Thibault Czarniak (327577)
 * @author Matthias Wyss (329884)
 * Interface de Serial-deserializer (Serde) pour la communication réseau
 */
public interface Serde<E> {

    /**
     * prend en argument l'objet à sérialiser et retoure la chaîne correspondante
     * @param serializableObject l'objet à sérialiser
     * @return la chaîne correspondante sérialisée
     */
    String serialize(E serializableObject);

    /**
     * prend en argument une chaîne sérialisée et retourne l'objet correspondant
     * @param serializedString la chaîne à désérialiser
     * @return l'objet correspondant
     */
    E deserialize(String serializedString);

    /**
     * méthode générique prenant en arguments une fonction de sérialisation et une fonction de désérialisation, et retournant le serde correspondant
     * @param serializer fonction de sérialisation
     * @param deserializer fonction de désérialisation
     * @param <T> paramètre de type de la méthode
     * @return le serde correspondant
     */
    static <T> Serde<T> of(Function<T, String> serializer, Function<String, T> deserializer){
        return new Serde<>(){

            @Override
            public String serialize(T serializableObject) {
                return serializer.apply(serializableObject);
            }

            @Override
            public T deserialize(String serializedString) {
                return deserializer.apply(serializedString);
            }
        };
    }

    /**
     * méthode générique prenant en argument la liste de toutes les valeurs d'un ensemble de valeurs énuméré et retournant le serde correspondant
     * @param list liste de toutes les valeurs d'un ensemble de valeurs énuméré
     * @param <T> type des valeurs de la liste
     * @return le serde correspondant
     */
    static <T> Serde<T> oneOf(List<T> list) {
        Preconditions.checkArgument(!list.isEmpty());
        return new Serde<>() {

            @Override
            public String serialize(T serializableObject) {
                return String.valueOf(list.indexOf(serializableObject));
            }

            @Override
            public T deserialize(String serializedString) {
                return list.get(Integer.parseInt(serializedString));
            }
        };
    }

    /**
     * méthode générique prenant en argument un serde et un caractère de séparation et retournant un serde capable de (dé)sérialiser des listes de valeurs (dé)sérialisées par le serde donné
     * @param serde serde servant à serialiser et déserialiser
     * @param cSep caractère de séparation
     * @param <T> type des valeurs de la liste
     * @return un serde capable de (dé)sérialiser des listes de valeurs (dé)sérialisées par le serde donné
     */
    static <T> Serde<List<T>> listOf(Serde<T> serde, char cSep) {
        return new Serde<>() {

            @Override
            public String serialize(List<T> serializableObject) {
                List<String> serializedStrings = new ArrayList<>();
                serializableObject.forEach(e -> serializedStrings.add(serde.serialize(e)));
                return String.join(String.valueOf(cSep), serializedStrings);
            }

            @Override
            public List<T> deserialize(String serializedString) {
                if(serializedString.isEmpty()){
                    return List.of();
                }
                List<T> deserializedValues = new ArrayList<>();
                String[] stringTable = serializedString.split(Pattern.quote(String.valueOf(cSep)), -1);
                for(String s : stringTable){
                    deserializedValues.add(serde.deserialize(s));
                }
                return deserializedValues;
            }
        };
    }

    /**
     * méthode générique prenant en argument un serde et un caractère de séparation et retournant un serde capable de (dé)sérialiser des SortedBag de valeurs (dé)sérialisées par le serde donné
     * @param serde serde servant à serialiser et déserialiser
     * @param cSep caractère de séparation
     * @param <T> type des valeurs du SortedBag
     * @return un serde capable de (dé)sérialiser des SortedBag de valeurs (dé)sérialisées par le serde donné
     */
    static <T extends Comparable<T>> Serde<SortedBag<T>> bagOf(Serde<T> serde, char cSep) {
        return new Serde<>() {

            @Override
            public String serialize(SortedBag<T> serializableObject) {
                List<T> serializedList = serializableObject.toList();
                return listOf(serde, cSep).serialize(serializedList);
            }

            @Override
            public SortedBag<T> deserialize(String serializedString) {
                List<T> deserializedString = listOf(serde, cSep).deserialize(serializedString);
                return SortedBag.of(deserializedString);
            }
        };
    }

}
