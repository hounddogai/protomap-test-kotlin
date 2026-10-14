package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakTargetEngine(newRegistry())
        val item = newRestBreakTarget("code-709", "label-377")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
