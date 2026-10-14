package com.acme.transport.safety.coaching.target

import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingManifestEngine()
        val item = newCoachingManifest("code-169", "name-917")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
