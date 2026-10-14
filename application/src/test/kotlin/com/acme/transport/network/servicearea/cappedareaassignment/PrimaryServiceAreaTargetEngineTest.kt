package com.acme.transport.network.servicearea.cappedareaassignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryServiceAreaTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryServiceAreaTargetEngine(newRegistry())
        val item = newPrimaryServiceAreaTarget("code-587", "title-640")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
