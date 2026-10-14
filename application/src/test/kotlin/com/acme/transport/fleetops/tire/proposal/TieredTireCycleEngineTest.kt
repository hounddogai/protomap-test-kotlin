package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredTireCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredTireCycleEngine(newRegistry())
        val item = newTieredTireCycle("code-665", "title-605")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
