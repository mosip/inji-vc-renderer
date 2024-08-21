import io.mosip.injivcrenderer.InjiVcRenderer
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(MockitoJUnitRunner::class)
class InjiVcRendererTest {

    private lateinit var renderer: InjiVcRenderer
    private lateinit var mockJsonObject: JSONObject

    @Before
    fun setUp() {
        renderer = InjiVcRenderer()
        mockJsonObject = mock(JSONObject::class.java)
    }

    @Test
    fun testGetValueFromData_ValidKeyPath() {
        val profileObject = mock(JSONObject::class.java)
        val userObject = mock(JSONObject::class.java)

        `when`(mockJsonObject.opt("user")).thenReturn(userObject)
        `when`(userObject.opt("profile")).thenReturn(profileObject)
        `when`(profileObject.opt("name")).thenReturn("John")

        val result = renderer.getValueFromData("user/profile/name", mockJsonObject)
        assertEquals("John", result)
    }

    @Test
    fun testGetValueFromData_NestedKeyPath() {
        val addressObject = mock(JSONObject::class.java)
        val userObject = mock(JSONObject::class.java)

        `when`(mockJsonObject.opt("user")).thenReturn(userObject)
        `when`(userObject.opt("address")).thenReturn(addressObject)
        `when`(addressObject.opt("city")).thenReturn("New York")

        val result = renderer.getValueFromData("user/address/city", mockJsonObject)
        assertEquals("New York", result)
    }

    @Test
    fun testGetValueFromData_InvalidKeyPath() {
        `when`(mockJsonObject.opt("user")).thenReturn(null)

        val result = renderer.getValueFromData("user/age", mockJsonObject)
        assertNull(result)
    }



}
