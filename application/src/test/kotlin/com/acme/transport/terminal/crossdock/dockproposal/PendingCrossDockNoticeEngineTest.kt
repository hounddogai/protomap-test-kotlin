package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingCrossDockNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingCrossDockNoticeEngine(newRegistry())
        val item = newPendingCrossDockNotice("code-503", "label-212")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
