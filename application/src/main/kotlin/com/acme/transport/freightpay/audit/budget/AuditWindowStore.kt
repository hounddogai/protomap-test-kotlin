package com.acme.transport.freightpay.audit.budget

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit windows by code. */
interface AuditWindowStore {
    fun find(code: String): AuditWindow?
    fun save(item: AuditWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditWindowStore(
    var items: MutableMap<String, AuditWindow>,
    var order: MutableList<String>,
) : AuditWindowStore {
    override fun find(code: String): AuditWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditWindow) {
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

    override fun list(): MutableList<AuditWindow> {
        val out = mutableListOf<AuditWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditWindowStore(): AuditWindowStore {
    return MemoryAuditWindowStore(mutableMapOf<String, AuditWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditWindowStore(
    var inner: AuditWindowStore,
    var cache: TtlCache<AuditWindow>,
    var hits: Int,
    var misses: Int,
) : AuditWindowStore {
    override fun find(code: String): AuditWindow? {
        val hit = (cache.get(code) as AuditWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditWindow> = inner.list()
}

fun newCachedAuditWindowStore(inner: AuditWindowStore, ttl: Int, clock: Clock): AuditWindowStore {
    return CachedAuditWindowStore(
        inner = inner,
        cache = newTtlCache<AuditWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit windows. Empty fields match all. */
data class AuditWindowQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditWindow): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditWindow>): MutableList<AuditWindow> {
        val matched = filter(items) { candidate: AuditWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditWindows(store: AuditWindowStore, query: AuditWindowQuery): MutableList<AuditWindow> {
    return query.apply(store.list())
}
