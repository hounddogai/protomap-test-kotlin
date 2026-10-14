package com.acme.transport.emissions.idling.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualIdlingScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualIdlingScoreEngine(newRegistry())
        val item = newManualIdlingScore("code-802", "name-638")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
