package com.acme.transport.safety.coaching.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyCoachingSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyCoachingSnapshotEngine(newRegistry())
        val item = newLegacyCoachingSnapshot("code-623", "title-427")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
