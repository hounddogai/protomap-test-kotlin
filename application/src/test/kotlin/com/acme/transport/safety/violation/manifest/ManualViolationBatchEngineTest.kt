package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualViolationBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualViolationBatchEngine(newRegistry())
        val item = newManualViolationBatch("code-498", "label-620")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
