package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakThresholdEngine(newRegistry())
        val item = newRestBreakThreshold("code-653", "name-748")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
