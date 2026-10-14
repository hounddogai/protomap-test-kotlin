package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissRecordEngine(newRegistry())
        val item = newNearMissRecord("code-553", "title-908")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
