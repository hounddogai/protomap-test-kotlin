package com.acme.transport.fleetops.tire.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalTireBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalTireBatchEngine(newRegistry())
        val item = newRegionalTireBatch("code-341", "label-681")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
