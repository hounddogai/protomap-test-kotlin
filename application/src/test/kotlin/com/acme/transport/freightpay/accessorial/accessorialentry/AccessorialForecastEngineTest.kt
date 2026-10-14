package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialForecastEngine(newRegistry())
        val item = newAccessorialForecast("code-23", "title-434")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
