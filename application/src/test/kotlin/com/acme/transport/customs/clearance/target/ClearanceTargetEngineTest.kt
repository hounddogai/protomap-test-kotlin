package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceTargetEngine(newRegistry())
        val item = newClearanceTarget("code-202", "title-797")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
