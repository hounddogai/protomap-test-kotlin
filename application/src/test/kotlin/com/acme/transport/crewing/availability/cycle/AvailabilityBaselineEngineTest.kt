package com.acme.transport.crewing.availability.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityBaselineEngine(newRegistry())
        val item = newAvailabilityBaseline("code-343", "title-939")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
