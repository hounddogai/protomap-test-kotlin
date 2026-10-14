package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment drafts by code. */
interface AppointmentDraftStore {
    fun find(code: String): AppointmentDraft?
    fun save(item: AppointmentDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentDraftStore(
    var items: MutableMap<String, AppointmentDraft>,
    var order: MutableList<String>,
) : AppointmentDraftStore {
    override fun find(code: String): AppointmentDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentDraft) {
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

    override fun list(): MutableList<AppointmentDraft> {
        val out = mutableListOf<AppointmentDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentDraftStore(): AppointmentDraftStore {
    return MemoryAppointmentDraftStore(mutableMapOf<String, AppointmentDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentDraftStore(
    var inner: AppointmentDraftStore,
    var cache: TtlCache<AppointmentDraft>,
    var hits: Int,
    var misses: Int,
) : AppointmentDraftStore {
    override fun find(code: String): AppointmentDraft? {
        val hit = (cache.get(code) as AppointmentDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentDraft> = inner.list()
}

fun newCachedAppointmentDraftStore(inner: AppointmentDraftStore, ttl: Int, clock: Clock): AppointmentDraftStore {
    return CachedAppointmentDraftStore(
        inner = inner,
        cache = newTtlCache<AppointmentDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment drafts. Empty fields match all. */
data class AppointmentDraftQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentDraft>): MutableList<AppointmentDraft> {
        val matched = filter(items) { candidate: AppointmentDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentDrafts(store: AppointmentDraftStore, query: AppointmentDraftQuery): MutableList<AppointmentDraft> {
    return query.apply(store.list())
}
