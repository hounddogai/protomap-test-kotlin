package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryHoldOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryHoldOverrideEngine()
        val item = newPrimaryHoldOverride("code-784", "title-381")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
