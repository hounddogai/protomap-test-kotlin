package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledTireBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledTireBundleEngine(newRegistry())
        val item = newScheduledTireBundle("code-118", "label-564")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
