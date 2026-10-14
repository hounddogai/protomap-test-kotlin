package com.acme.transport.safety.nearmiss.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default near miss audits by code. */
interface DefaultNearMissAuditStore {
    fun find(code: String): DefaultNearMissAudit?
    fun save(item: DefaultNearMissAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultNearMissAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultNearMissAuditStore(
    var items: MutableMap<String, DefaultNearMissAudit>,
    var order: MutableList<String>,
) : DefaultNearMissAuditStore {
    override fun find(code: String): DefaultNearMissAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultNearMissAudit) {
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

    override fun list(): MutableList<DefaultNearMissAudit> {
        val out = mutableListOf<DefaultNearMissAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultNearMissAuditStore(): DefaultNearMissAuditStore {
    return MemoryDefaultNearMissAuditStore(
        mutableMapOf<String, DefaultNearMissAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultNearMissAuditStore(
    var inner: DefaultNearMissAuditStore,
    var cache: TtlCache<DefaultNearMissAudit>,
    var hits: Int,
    var misses: Int,
) : DefaultNearMissAuditStore {
    override fun find(code: String): DefaultNearMissAudit? {
        val hit = (cache.get(code) as DefaultNearMissAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultNearMissAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultNearMissAudit> = inner.list()
}

fun newCachedDefaultNearMissAuditStore(inner: DefaultNearMissAuditStore, ttl: Int, clock: Clock): DefaultNearMissAuditStore {
    return CachedDefaultNearMissAuditStore(
        inner = inner,
        cache = newTtlCache<DefaultNearMissAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default near miss audits. Empty fields match all. */
data class DefaultNearMissAuditQuery(
    var source: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultNearMissAudit): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<DefaultNearMissAudit>): MutableList<DefaultNearMissAudit> {
        val matched = filter(items) { candidate: DefaultNearMissAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultNearMissAudits(store: DefaultNearMissAuditStore, query: DefaultNearMissAuditQuery): MutableList<DefaultNearMissAudit> {
    return query.apply(store.list())
}
