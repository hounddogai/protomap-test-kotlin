package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingZoneReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingZoneReviewEngine(newRegistry())
        val item = newRollingZoneReview("code-399", "title-404")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
