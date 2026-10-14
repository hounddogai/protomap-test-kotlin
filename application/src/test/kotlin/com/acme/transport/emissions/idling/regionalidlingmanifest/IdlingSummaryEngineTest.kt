package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingSummaryEngine(newRegistry())
        val item = newIdlingSummary("code-873", "title-105")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
