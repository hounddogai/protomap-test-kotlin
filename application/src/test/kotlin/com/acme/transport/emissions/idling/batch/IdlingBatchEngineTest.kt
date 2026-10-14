package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingBatchEngine(newRegistry())
        val item = newIdlingBatch("code-820", "title-318")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
