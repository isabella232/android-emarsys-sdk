package com.emarsys.core.request.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.database.CoreSQLiteDatabase;
import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.helper.CoreDbHelper;
import com.emarsys.core.database.repository.specification.QueryAll;
import com.emarsys.core.testUtil.DatabaseTestUtils;
import com.emarsys.core.testUtil.TimeoutUtils;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_HEADERS;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_METHOD;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_PAYLOAD;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_REQUEST_ID;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_TIMESTAMP;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_TTL;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_URL;
import static com.emarsys.core.util.serialization.SerializationUtils.serializableToBlob;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestModelRepositoryTest {

    public static final String URL_EMARSYS = "https://www.emarsys.com";

    static {
        mock(Cursor.class);
    }

    private RequestModel request;
    private RequestModelRepository repository;
    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        DatabaseTestUtils.INSTANCE.deleteCoreDatabase();

        context = InstrumentationRegistry.getContext();
        repository = new RequestModelRepository(context);

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("payload1", "payload_value1");
        payload.put("payload2", "payload_value2");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("header1", "header_value1");
        headers.put("header2", "header_value2");

        request = new RequestModel("https://www.google.com", RequestMethod.GET, payload, headers, System.currentTimeMillis(), 600, "idka");
    }

    @Test
    public void testContentValuesFromItem() {
        ContentValues result = repository.contentValuesFromItem(request);

        assertEquals(request.getId(), result.getAsString(REQUEST_COLUMN_NAME_REQUEST_ID));
        assertEquals(request.getMethod().name(), result.getAsString(REQUEST_COLUMN_NAME_METHOD));
        assertEquals(request.getUrl().toString(), result.getAsString(REQUEST_COLUMN_NAME_URL));
        assertArrayEquals(serializableToBlob(request.getHeaders()), result.getAsByteArray(REQUEST_COLUMN_NAME_HEADERS));
        assertArrayEquals(serializableToBlob(request.getPayload()), result.getAsByteArray(REQUEST_COLUMN_NAME_PAYLOAD));
        assertEquals(request.getTimestamp(), (long) result.getAsLong(REQUEST_COLUMN_NAME_TIMESTAMP));
        assertEquals(request.getTtl(), (long) result.getAsLong(REQUEST_COLUMN_NAME_TTL));
    }

    @Test
    public void testItemFromCursor() {
        Cursor cursor = mock(Cursor.class);

        when(cursor.getColumnIndex(REQUEST_COLUMN_NAME_REQUEST_ID)).thenReturn(0);
        when(cursor.getString(0)).thenReturn(request.getId());
        when(cursor.getColumnIndex(REQUEST_COLUMN_NAME_METHOD)).thenReturn(1);
        when(cursor.getString(1)).thenReturn(request.getMethod().name());
        when(cursor.getColumnIndex(REQUEST_COLUMN_NAME_URL)).thenReturn(2);
        when(cursor.getString(2)).thenReturn(request.getUrl().toString());
        when(cursor.getColumnIndex(REQUEST_COLUMN_NAME_HEADERS)).thenReturn(3);
        when(cursor.getBlob(3)).thenReturn(serializableToBlob(request.getHeaders()));
        when(cursor.getColumnIndex(REQUEST_COLUMN_NAME_PAYLOAD)).thenReturn(4);
        when(cursor.getBlob(4)).thenReturn(serializableToBlob(request.getPayload()));
        when(cursor.getColumnIndex(REQUEST_COLUMN_NAME_TIMESTAMP)).thenReturn(5);
        when(cursor.getLong(5)).thenReturn(request.getTimestamp());
        when(cursor.getColumnIndex(REQUEST_COLUMN_NAME_TTL)).thenReturn(6);
        when(cursor.getLong(6)).thenReturn(request.getTtl());

        RequestModel result = repository.itemFromCursor(cursor);

        assertEquals(request, result);
    }

    @Test
    public void testQuery_shouldFallBack_toEmptyMap_shouldDeserializationFail() throws JSONException {
        initializeDatabaseWithCorrectAndIncorrectData();

        List<RequestModel> result = repository.query(new QueryAll(DatabaseContract.REQUEST_TABLE_NAME));

        RequestModel model1 = new RequestModel(URL_EMARSYS, RequestMethod.POST, new HashMap<String, Object>(), new HashMap<String, String>(), 100, 300, "id1");
        RequestModel model2 = new RequestModel(URL_EMARSYS, RequestMethod.POST, createAttribute(), new HashMap<String, String>(), 100, 300, "id2");
        List<RequestModel> expected = Arrays.asList(model1, model2);

        assertEquals(expected, result);
    }

    private void initializeDatabaseWithCorrectAndIncorrectData() throws JSONException {
        CoreDbHelper dbHelper = new CoreDbHelper(context);
        CoreSQLiteDatabase db = dbHelper.getWritableCoreDatabase();

        String jsonString = "{'key1': 'value1', 'key2':321}";

        HashMap<String, Object> mapAttribute = createAttribute();

        ContentValues record1 = createContentValues("id1", jsonString);
        ContentValues record2 = createContentValues("id2", mapAttribute);

        db.insert(DatabaseContract.REQUEST_TABLE_NAME, null, record1);
        db.insert(DatabaseContract.REQUEST_TABLE_NAME, null, record2);
    }

    private HashMap<String, Object> createAttribute() {
        HashMap<String, Object> mapAttribute = new HashMap<>();
        mapAttribute.put("key1", "value2");
        mapAttribute.put("key2", false);
        mapAttribute.put("key3", 1000);
        return mapAttribute;
    }

    private ContentValues createContentValues(String id, Object attributes) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(REQUEST_COLUMN_NAME_REQUEST_ID, id);
        contentValues.put(REQUEST_COLUMN_NAME_METHOD, RequestMethod.POST.toString());
        contentValues.put(REQUEST_COLUMN_NAME_URL, URL_EMARSYS);
        contentValues.put(REQUEST_COLUMN_NAME_HEADERS, serializableToBlob(new HashMap<>()));
        contentValues.put(REQUEST_COLUMN_NAME_PAYLOAD, serializableToBlob(attributes));
        contentValues.put(REQUEST_COLUMN_NAME_TIMESTAMP, 100);
        contentValues.put(REQUEST_COLUMN_NAME_TTL, 300);
        return contentValues;
    }

}