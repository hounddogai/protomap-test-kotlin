package com.acme.transport.freightpay.accessorial.accessorialentry

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedAccessorialEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedAccessorialEntryEngine()
        val item = newCappedAccessorialEntry("code-746", "name-804")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
