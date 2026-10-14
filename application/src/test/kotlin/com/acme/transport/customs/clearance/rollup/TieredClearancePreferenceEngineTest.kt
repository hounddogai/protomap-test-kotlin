package com.acme.transport.customs.clearance.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredClearancePreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredClearancePreferenceEngine(newRegistry())
        val item = newTieredClearancePreference("code-554", "label-775")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
