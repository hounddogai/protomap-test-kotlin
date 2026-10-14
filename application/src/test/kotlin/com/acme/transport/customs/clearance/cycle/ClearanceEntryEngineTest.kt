package com.acme.transport.customs.clearance.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceEntryEngine(newRegistry())
        val item = newClearanceEntry("code-679", "name-126")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
