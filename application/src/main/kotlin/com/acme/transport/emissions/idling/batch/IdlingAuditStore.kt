package com.acme.transport.emissions.idling.batch

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling audits by code. */
interface IdlingAuditStore {
    fun find(code: String): IdlingAudit?
    fun save(item: IdlingAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingAuditStore(
    var items: MutableMap<String, IdlingAudit>,
    var order: MutableList<String>,
) : IdlingAuditStore {
    override fun find(code: String): IdlingAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingAudit) {
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

    override fun list(): MutableList<IdlingAudit> {
        val out = mutableListOf<IdlingAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingAuditStore(): IdlingAuditStore {
    return MemoryIdlingAuditStore(mutableMapOf<String, IdlingAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingAuditStore(
    var inner: IdlingAuditStore,
    var cache: TtlCache<IdlingAudit>,
    var hits: Int,
    var misses: Int,
) : IdlingAuditStore {
    override fun find(code: String): IdlingAudit? {
        val hit = (cache.get(code) as IdlingAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingAudit> = inner.list()
}

fun newCachedIdlingAuditStore(inner: IdlingAuditStore, ttl: Int, clock: Clock): IdlingAuditStore {
    return CachedIdlingAuditStore(
        inner = inner,
        cache = newTtlCache<IdlingAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling audits. Empty fields match all. */
data class IdlingAuditQuery(
    var category: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingAudit): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingAudit>): MutableList<IdlingAudit> {
        val matched = filter(items) { candidate: IdlingAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingAudits(store: IdlingAuditStore, query: IdlingAuditQuery): MutableList<IdlingAudit> {
    return query.apply(store.list())
}
