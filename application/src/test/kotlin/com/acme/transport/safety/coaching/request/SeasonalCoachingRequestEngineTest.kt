package com.acme.transport.safety.coaching.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalCoachingRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalCoachingRequestEngine(newRegistry())
        val item = newSeasonalCoachingRequest("code-109", "title-724")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
