package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredLanePlanOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredLanePlanOverrideEngine(newRegistry())
        val item = newTieredLanePlanOverride("code-975", "title-565")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
