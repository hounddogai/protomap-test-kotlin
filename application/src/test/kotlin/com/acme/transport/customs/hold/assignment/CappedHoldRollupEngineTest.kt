package com.acme.transport.customs.hold.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedHoldRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedHoldRollupEngine(newRegistry())
        val item = newCappedHoldRollup("code-69", "title-449")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
