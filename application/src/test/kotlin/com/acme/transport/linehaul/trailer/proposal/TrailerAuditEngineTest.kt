package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerAuditEngine(newRegistry())
        val item = newTrailerAudit("code-926", "name-764")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
