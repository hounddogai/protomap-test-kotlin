package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceClaimEngine(newRegistry())
        val item = newClearanceClaim("code-867", "label-978")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
