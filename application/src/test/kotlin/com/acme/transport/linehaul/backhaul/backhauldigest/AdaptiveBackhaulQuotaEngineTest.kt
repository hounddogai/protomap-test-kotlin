package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveBackhaulQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveBackhaulQuotaEngine(newRegistry())
        val item = newAdaptiveBackhaulQuota("code-206", "name-636")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
