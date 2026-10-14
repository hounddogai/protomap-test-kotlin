package com.acme.transport.network.servicearea.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaRollupEngine(newRegistry())
        val item = newServiceAreaRollup("code-808", "label-37")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
