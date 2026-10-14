package com.acme.transport.crewing.availability.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityBundleEngine(newRegistry())
        val item = newAvailabilityBundle("code-887", "label-631")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
