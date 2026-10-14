package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps capped redelivery windows by code. */
interface CappedRedeliveryWindowStore {
    fun find(code: String): CappedRedeliveryWindow?
    fun save(item: CappedRedeliveryWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedRedeliveryWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedRedeliveryWindowStore(
    var items: MutableMap<String, CappedRedeliveryWindow>,
    var order: MutableList<String>,
) : CappedRedeliveryWindowStore {
    override fun find(code: String): CappedRedeliveryWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedRedeliveryWindow) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<CappedRedeliveryWindow> {
        val out = mutableListOf<CappedRedeliveryWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedRedeliveryWindowStore(): CappedRedeliveryWindowStore {
    return MemoryCappedRedeliveryWindowStore(
        mutableMapOf<String, CappedRedeliveryWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedRedeliveryWindowStore(
    var inner: CappedRedeliveryWindowStore,
    var cache: TtlCache<CappedRedeliveryWindow>,
    var hits: Int,
    var misses: Int,
) : CappedRedeliveryWindowStore {
    override fun find(code: String): CappedRedeliveryWindow? {
        val hit = (cache.get(code) as CappedRedeliveryWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedRedeliveryWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedRedeliveryWindow> = inner.list()
}

fun newCachedCappedRedeliveryWindowStore(inner: CappedRedeliveryWindowStore, ttl: Int, clock: Clock): CappedRedeliveryWindowStore {
    return CachedCappedRedeliveryWindowStore(
        inner = inner,
        cache = newTtlCache<CappedRedeliveryWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped redelivery windows. Empty fields match all. */
data class CappedRedeliveryWindowQuery(
    var label: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedRedeliveryWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedRedeliveryWindow>): MutableList<CappedRedeliveryWindow> {
        val matched = filter(items) { candidate: CappedRedeliveryWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedRedeliveryWindows(store: CappedRedeliveryWindowStore, query: CappedRedeliveryWindowQuery): MutableList<CappedRedeliveryWindow> {
    return query.apply(store.list())
}
