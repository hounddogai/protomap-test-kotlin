package com.acme.transport.customs.hold.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictHoldBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictHoldBundleEngine(newRegistry())
        val item = newStrictHoldBundle("code-487", "name-166")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
