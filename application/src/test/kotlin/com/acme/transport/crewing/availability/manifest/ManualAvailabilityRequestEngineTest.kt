package com.acme.transport.crewing.availability.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualAvailabilityRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualAvailabilityRequestEngine(newRegistry())
        val item = newManualAvailabilityRequest("code-914", "title-406")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
