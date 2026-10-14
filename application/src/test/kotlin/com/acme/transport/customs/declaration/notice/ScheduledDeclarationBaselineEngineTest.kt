package com.acme.transport.customs.declaration.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDeclarationBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDeclarationBaselineEngine(newRegistry())
        val item = newScheduledDeclarationBaseline("code-336", "name-975")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
