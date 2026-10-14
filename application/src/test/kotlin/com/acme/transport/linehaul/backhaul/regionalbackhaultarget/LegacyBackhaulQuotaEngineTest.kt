package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyBackhaulQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyBackhaulQuotaEngine(newRegistry())
        val item = newLegacyBackhaulQuota("code-546", "title-229")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
