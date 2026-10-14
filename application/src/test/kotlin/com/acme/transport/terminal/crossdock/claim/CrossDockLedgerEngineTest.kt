package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockLedgerEngine(newRegistry())
        val item = newCrossDockLedger("code-203", "title-201")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
