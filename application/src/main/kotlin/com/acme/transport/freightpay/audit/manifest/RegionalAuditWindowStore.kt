package com.acme.transport.freightpay.audit.manifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional audit windows by code. */
interface RegionalAuditWindowStore {
    fun find(code: String): RegionalAuditWindow?
    fun save(item: RegionalAuditWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalAuditWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalAuditWindowStore(
    var items: MutableMap<String, RegionalAuditWindow>,
    var order: MutableList<String>,
) : RegionalAuditWindowStore {
    override fun find(code: String): RegionalAuditWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalAuditWindow) {
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

    override fun list(): MutableList<RegionalAuditWindow> {
        val out = mutableListOf<RegionalAuditWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalAuditWindowStore(): RegionalAuditWindowStore {
    return MemoryRegionalAuditWindowStore(mutableMapOf<String, RegionalAuditWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalAuditWindowStore(
    var inner: RegionalAuditWindowStore,
    var cache: TtlCache<RegionalAuditWindow>,
    var hits: Int,
    var misses: Int,
) : RegionalAuditWindowStore {
    override fun find(code: String): RegionalAuditWindow? {
        val hit = (cache.get(code) as RegionalAuditWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalAuditWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalAuditWindow> = inner.list()
}

fun newCachedRegionalAuditWindowStore(inner: RegionalAuditWindowStore, ttl: Int, clock: Clock): RegionalAuditWindowStore {
    return CachedRegionalAuditWindowStore(
        inner = inner,
        cache = newTtlCache<RegionalAuditWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional audit windows. Empty fields match all. */
data class RegionalAuditWindowQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalAuditWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalAuditWindow>): MutableList<RegionalAuditWindow> {
        val matched = filter(items) { candidate: RegionalAuditWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalAuditWindows(store: RegionalAuditWindowStore, query: RegionalAuditWindowQuery): MutableList<RegionalAuditWindow> {
    return query.apply(store.list())
}
