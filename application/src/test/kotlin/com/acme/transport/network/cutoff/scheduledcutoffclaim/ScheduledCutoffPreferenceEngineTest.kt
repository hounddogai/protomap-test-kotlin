package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledCutoffPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledCutoffPreferenceEngine(newRegistry())
        val item = newScheduledCutoffPreference("code-47", "title-346")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
