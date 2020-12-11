package ovh.bailon.foodnet;

public interface IFoodnetDBHelper {
    void add(OpenDating openDating);

    void requestGet(long id);

    void requestGetAll();

    void requestGetAll(int location);

    int update(OpenDating openDating);

    void delete(OpenDating openDating);

    void registerOnDataChange(OnDataEventListener listener);
}
